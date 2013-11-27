/**
 * MobWrite extension to support editable div sharing
 *
 * @author Evgeny Naumenko.
 */

/**
 * Checks to see if the provided node is still part of the DOM.
 * @param {Node} node DOM node to verify.
 * @return {boolean} Is this node part of a DOM?
 * @private
 */
mobwrite.validNode_ = function (node) {
    while (node.parentNode) {
        node = node.parentNode;
    }
    // The topmost node should be type 9, a document.
    return node.nodeType == 9;
};

mobwrite.shareDiv = function (node) {
    // Call our prototype's constructor.
    mobwrite.shareObj.apply(this, [node.id]);
    this.element = node;
};


// Div shared object's parent is a shareObj.
mobwrite.shareDiv.prototype = new mobwrite.shareObj('');


/**
 * Retrieve the user's text.
 * @return {string} Plaintext content.
 */
mobwrite.shareDiv.prototype.getClientText = function () {
    if (!mobwrite.validNode_(this.element)) {
        mobwrite.unshare(this.file);
    }
    return mobwrite.shareDiv.normalizeLinebreaks_(this.element.innerText);
};


/**
 * Set the user's text.
 * @param {string} text New text
 */
mobwrite.shareDiv.prototype.setClientText = function (text) {
    this.element.innerText = text;
    this.fireChange(this.element);
};

/**
 * Tiny JQuery extension to make other user's cursors blink
 */
(function ($) {
    $.fn.blink = function () {
        return this.each(function () {
            var obj = $(this);
            setInterval(function () {
                if ($(obj).css("visibility") == "visible") {
                    $(obj).css('visibility', 'hidden');
                }
                else {
                    $(obj).css('visibility', 'visible');
                }
            }, 750);
        });
    }
}(jQuery));

mobwrite.shareDiv.prototype.isOrContains = function (node, container) {
    while (node) {
        if (node === container) {
            return true;
        }
        node = node.parentNode;
    }
    return false;
};

mobwrite.shareDiv.prototype.elementContainsSelection = function (el) {
    var sel;
    if (window.getSelection) {
        sel = window.getSelection();
        if (sel.rangeCount > 0) {
            for (var i = 0; i < sel.rangeCount; ++i) {
                if (!this.isOrContains(sel.getRangeAt(i).commonAncestorContainer, el)) {
                    return false;
                }
            }
            return true;
        }
    } else if ((sel = document.selection) && sel.type != "Control") {
        return this.isOrContains(sel.createRange().parentElement(), el);
    }
    return false;
};

mobwrite.shareDiv.prototype.previousNode = function (baseNode) {
    if (baseNode.previousSibling) {
        return baseNode.previousSibling;
    } else {
        var parent = baseNode.parentElement;
        if (parent && parent != this.element && parent.previousSibling) {
            if (parent.previousSibling.childNodes[0]) {
                return parent.previousSibling.childNodes[0];
            } else {
                return parent.previousSibling;
            }
        }
    }
};

mobwrite.shareDiv.prototype.getCursorOffset = function () {
    if (this.elementContainsSelection(this.element)) {
        var baseNode = window.getSelection().baseNode;
        var offset = window.getSelection().baseOffset;
        while (baseNode = this.previousNode(baseNode)) {
            if (baseNode.data) {
                offset += baseNode.data.length;
            }
            if (baseNode.tagName != 'DIV') {
                offset++;
            }
        }
        return offset;
    } else {
        return 0;
    }
};

/**
 * Asks the shareObj to synchronize.  Computes client-made changes since
 * previous postback. Return nothing to skip this synchronization.
 * @return {string} Commands to be sent to the server.
 */
mobwrite.shareDiv.prototype.syncText = function () {
    var clientText = this.getClientText();
    var syncRequired = false;
    if (this.deltaOk) {
        // The last delta postback from the server to this shareObj was successful.
        // Send a compressed delta.
        var diffs = this.dmp.diff_main(this.shadowText, clientText, true);
        if (diffs.length > 2) {
            this.dmp.diff_cleanupSemantic(diffs);
            this.dmp.diff_cleanupEfficiency(diffs);
        }
        var changed = diffs.length != 1 || diffs[0][0] != DIFF_EQUAL;
        if (changed) {
            mobwrite.clientChange_ = true;
            this.shadowText = clientText;
        }
        // Don't bother appending a no-change diff onto the stack if the stack
        // already contains something.
        if (changed || !this.editStack.length) {
            var action = (this.mergeChanges ? 'd:' : 'D:') + this.clientVersion +
                ':' + this.dmp.diff_toDelta(diffs);
            this.editStack.push([this.clientVersion, action]);
            this.clientVersion++;
            this.onSentDiff(diffs);
        }
        syncRequired = changed || mobwrite.force
    } else {
        // The last delta postback from the server to this shareObj didn't match.
        // Send a full text dump to get back in sync.  This will result in any
        // changes since the last postback being wiped out. :(
        this.shadowText = clientText;
        this.clientVersion++;
        var action = 'r:' + this.clientVersion + ':' +
            encodeURI(clientText).replace(/%20/g, ' ');
        // Append the action to the edit stack.
        this.editStack.push([this.clientVersion, action]);
        // Sending a raw dump will put us back in sync.
        // Set deltaOk to true in case this sync fails to connect, in which case
        // the following sync(s) should be a delta, not more raw dumps.
        this.deltaOk = true;
        syncRequired = true;
    }

    if (syncRequired) {
        mobwrite.force = false;
        // Create the output starting with the file statement, followed by the edits.
        var data = 'F:' + this.serverVersion + ':' +
            mobwrite.idPrefix + this.file + '\n';
        for (var x = 0; x < this.editStack.length; x++) {
            data += this.editStack[x][1] + '\n';
        }
        //add cursor position information
        data += 'c::' + this.getCursorOffset() + '\n';
        // Opera doesn't know how to encode char 0. (fixed in Opera 9.63)
        return data.replace(/\x00/g, '%00');
    }
    //skip this sync
};

/**
 * Modify the user's plaintext by applying a series of patches against it.
 * @param {Array.<patch_obj>} patches Array of Patch objects.
 */
mobwrite.shareDiv.prototype.patchClientText = function (patches) {
    // Set some constants which tweak the matching behaviour.
    // Maximum distance to search from expected location.
    this.dmp.Match_Distance = 1000;
    // At what point is no match declared (0.0 = perfection, 1.0 = very loose)
    this.dmp.Match_Threshold = 0.6;

    var oldClientText = this.getClientText();
    var cursor = this.captureCursor_();
    // Pack the cursor offsets into an array to be adjusted.
    // See http://neil.fraser.name/writing/cursor/
    var offsets = [];
    if (cursor) {
        offsets[0] = cursor.startOffset;
        if ('endOffset' in cursor) {
            offsets[1] = cursor.endOffset;
        }
    }
    var newClientText = this.patch_apply_(patches, oldClientText, offsets);
    // Set the new text only if there is a change to be made.
    if (oldClientText != newClientText) {
        this.setClientText(newClientText);
        if (cursor) {
            // Unpack the offset array.
            cursor.startOffset = offsets[0];
            if (offsets.length > 1) {
                cursor.endOffset = offsets[1];
                if (cursor.startOffset >= cursor.endOffset) {
                    cursor.collapsed = true;
                }
            }
            this.restoreCursor_(cursor);
        }
    }
};


/**
 * Merge a set of patches onto the text.  Return a patched text.
 * @param {Array.<patch_obj>} patches Array of patch objects.
 * @param {string} text Old text.
 * @param {Array.<number>} offsets Offset indices to adjust.
 * @return {string} New text.
 */
mobwrite.shareDiv.prototype.patch_apply_ =
    function (patches, text, offsets) {
        if (patches.length == 0) {
            return text;
        }

        // Deep copy the patches so that no changes are made to originals.
        patches = this.dmp.patch_deepCopy(patches);
        var nullPadding = this.dmp.patch_addPadding(patches);
        text = nullPadding + text + nullPadding;

        this.dmp.patch_splitMax(patches);
        // delta keeps track of the offset between the expected and actual location
        // of the previous patch.  If there are patches expected at positions 10 and
        // 20, but the first patch was found at 12, delta is 2 and the second patch
        // has an effective expected position of 22.
        var delta = 0;
        for (var x = 0; x < patches.length; x++) {
            var expected_loc = patches[x].start2 + delta;
            var text1 = this.dmp.diff_text1(patches[x].diffs);
            var start_loc;
            var end_loc = -1;
            if (text1.length > this.dmp.Match_MaxBits) {
                // patch_splitMax will only provide an oversized pattern in the case of
                // a monster delete.
                start_loc = this.dmp.match_main(text,
                    text1.substring(0, this.dmp.Match_MaxBits), expected_loc);
                if (start_loc != -1) {
                    end_loc = this.dmp.match_main(text,
                        text1.substring(text1.length - this.dmp.Match_MaxBits),
                        expected_loc + text1.length - this.dmp.Match_MaxBits);
                    if (end_loc == -1 || start_loc >= end_loc) {
                        // Can't find valid trailing context.  Drop this patch.
                        start_loc = -1;
                    }
                }
            } else {
                start_loc = this.dmp.match_main(text, text1, expected_loc);
            }
            if (start_loc == -1) {
                // No match found.  :(
                if (mobwrite.debug) {
                    window.console.warn('Patch failed: ' + patches[x]);
                }
                // Subtract the delta for this failed patch from subsequent patches.
                delta -= patches[x].length2 - patches[x].length1;
            } else {
                // Found a match.  :)
                if (mobwrite.debug) {
                    window.console.info('Patch OK.');
                }
                delta = start_loc - expected_loc;
                var text2;
                if (end_loc == -1) {
                    text2 = text.substring(start_loc, start_loc + text1.length);
                } else {
                    text2 = text.substring(start_loc, end_loc + this.dmp.Match_MaxBits);
                }
                // Run a diff to get a framework of equivalent indices.
                var diffs = this.dmp.diff_main(text1, text2, false);
                if (text1.length > this.dmp.Match_MaxBits &&
                    this.dmp.diff_levenshtein(diffs) / text1.length >
                        this.dmp.Patch_DeleteThreshold) {
                    // The end points match, but the content is unacceptably bad.
                    if (mobwrite.debug) {
                        window.console.warn('Patch contents mismatch: ' + patches[x]);
                    }
                } else {
                    var index1 = 0;
                    var index2;
                    for (var y = 0; y < patches[x].diffs.length; y++) {
                        var mod = patches[x].diffs[y];
                        if (mod[0] !== DIFF_EQUAL) {
                            index2 = this.dmp.diff_xIndex(diffs, index1);
                        }
                        if (mod[0] === DIFF_INSERT) {  // Insertion
                            text = text.substring(0, start_loc + index2) + mod[1] +
                                text.substring(start_loc + index2);
                            for (var i = 0; i < offsets.length; i++) {
                                if (offsets[i] + nullPadding.length > start_loc + index2) {
                                    offsets[i] += mod[1].length;
                                }
                            }
                        } else if (mod[0] === DIFF_DELETE) {  // Deletion
                            var del_start = start_loc + index2;
                            var del_end = start_loc + this.dmp.diff_xIndex(diffs,
                                index1 + mod[1].length);
                            text = text.substring(0, del_start) + text.substring(del_end);
                            for (var i = 0; i < offsets.length; i++) {
                                if (offsets[i] + nullPadding.length > del_start) {
                                    if (offsets[i] + nullPadding.length < del_end) {
                                        offsets[i] = del_start - nullPadding.length;
                                    } else {
                                        offsets[i] -= del_end - del_start;
                                    }
                                }
                            }
                        }
                        if (mod[0] !== DIFF_DELETE) {
                            index1 += mod[1].length;
                        }
                    }
                }
            }
        }
        // Strip the padding off.
        text = text.substring(nullPadding.length, text.length - nullPadding.length);
        return text;
    };

function toHtml(text) {
    return text.replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '\n<br>');
}

//cursors supposed to asc sorted
mobwrite.shareDiv.prototype.applyCursors = function (cursors) {
    if (cursors.length > 0) {
        var shadow = $('.shadow');
        var text = this.getClientText();
        var chunks = [];
        var ceil = 0;
        var floor = 0;
        for (var i = 0; i < cursors.length; i++) {
            floor = ceil;
            ceil = cursors[i];
            chunks.push(toHtml(text.substr(floor, ceil)));
        }
        chunks.push(toHtml(text.substr(ceil, text.length)));
        shadow.html(chunks.join('<span class="cursor"></span>'));
        jQuery('.cursor').blink();
    }
};

/**
 * Record information regarding the current cursor.
 * @return {Object?} Context information of the cursor.
 * @private
 */
mobwrite.shareDiv.prototype.captureCursor_ = function () {
    var padLength = this.dmp.Match_MaxBits / 2;  // Normally 16.
    var text = this.element.innerText;
    var cursor = {};
    var curPosition = this.getCursorOffset();
    if (this.elementContainsSelection(this.element)) {  // W3
        try {
            var selectionStart = curPosition;
            var selectionEnd = curPosition;
        } catch (e) {
            // No cursor; the element may be "display:none".
            return null;
        }
        cursor.startPrefix = text.substring(selectionStart - padLength, selectionStart);
        cursor.startSuffix = text.substring(selectionStart, selectionStart + padLength);
        cursor.startOffset = selectionStart;
    }
    return cursor;
};


/**
 * Attempt to restore the cursor's location.
 * @param {Object} cursor Context information of the cursor.
 * @private
 */
mobwrite.shareDiv.prototype.restoreCursor_ = function (cursor) {
    if (!cursor || !cursor.startOffset){
        return; // no cursor has been captured
    }
    // Set some constants which tweak the matching behaviour.
    // Maximum distance to search from expected location.
    this.dmp.Match_Distance = 1000;
    // At what point is no match declared (0.0 = perfection, 1.0 = very loose)
    this.dmp.Match_Threshold = 0.9;

    var padLength = this.dmp.Match_MaxBits / 2;  // Normally 16.
    var newText = this.element.innerText;

    // Find the start of the selection in the new text.
    var pattern1 = cursor.startPrefix + cursor.startSuffix;
    var pattern2, diff;
    var cursorStartPoint = this.dmp.match_main(newText, pattern1,
        cursor.startOffset - padLength);
    if (cursorStartPoint !== null) {
        pattern2 = newText.substring(cursorStartPoint,
            cursorStartPoint + pattern1.length);
        //alert(pattern1 + '\nvs\n' + pattern2);
        // Run a diff to get a framework of equivalent indicies.
        diff = this.dmp.diff_main(pattern1, pattern2, false);
        cursorStartPoint += this.dmp.diff_xIndex(diff, cursor.startPrefix.length);
    }

    if (cursorStartPoint === null) {
        // Jump to the offset of start.
        cursorStartPoint = cursor.startOffset;
    }

    // Restore selection.
    var range = document.createRange();
    var sel = window.getSelection();

    if (this.elementContainsSelection(this.element)) {  // W3
        var children = this.element.childNodes;
        for (var i = 0; i < children.length; i++) {
            var baseNode = children[i];
            if (baseNode.tagName == 'DIV'){
                baseNode = baseNode.childNodes[0];
            }
            if (baseNode.data){ // text node
               if (cursorStartPoint - baseNode.data.length < 0){
                   range.setStart(baseNode, cursorStartPoint);
               } else {
                   cursorStartPoint -= baseNode.data.length;
               }
            } else { // must be a <br>
                if (cursorStartPoint == 0){
                    range.setStart(baseNode, 0);
                } else {
                    cursorStartPoint--;
                }
            }
        }
        range.collapse(true);
        sel.removeAllRanges();
        sel.addRange(range);
    }
};


/**
 * Ensure that all linebreaks are LF
 * @param {string} text Text with unknown line breaks
 * @return {string} Text with normalized linebreaks
 * @private
 */
mobwrite.shareDiv.normalizeLinebreaks_ = function (text) {
    return text.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
};


/**
 * Handler to accept text fields as elements that can be shared.
 * If the element is a textarea, text or password input, create a new
 * sharing object.
 * @param {*} node Object or ID of object to share.
 * @return {Object?} A sharing object or null.
 */
mobwrite.shareDiv.shareHandler = function (node) {
    if (typeof node == 'string') {
        node = document.getElementById(node);
    }
    if (node && 'tagName' in node && node.tagName == 'DIV') {
        if (mobwrite.UA_webkit) {
            // Safari needs to track which text element has the focus.
            node.addEventListener('focus', function () {
                    this.activeElement = true;
                },
                false);
            node.addEventListener('blur', function () {
                    this.activeElement = false;
                },
                false);
            node.activeElement = false;
        }
        return new mobwrite.shareDiv(node);
    }
    return null;
};


// Register this shareHandler with MobWrite.
mobwrite.shareHandlers.push(mobwrite.shareDiv.shareHandler);
