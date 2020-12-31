package keystone.api.tools.interfaces;

import keystone.api.SelectionBox;

/*
Perform a function on every selection box
 */
public interface ISelectionBoxTool extends IKeystoneTool
{
    void process(SelectionBox box);
}
