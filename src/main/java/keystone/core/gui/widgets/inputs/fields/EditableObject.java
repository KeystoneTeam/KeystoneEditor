package keystone.core.gui.widgets.inputs.fields;

public class EditableObject
{
    private boolean editorDirtied;

    public final void dirtyEditor() { editorDirtied = true; }
    public final boolean isEditorDirtied() { return editorDirtied; }
    public final void undirtyEditor() { editorDirtied = false; }
}
