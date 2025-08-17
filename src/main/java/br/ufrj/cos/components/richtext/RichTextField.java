package br.ufrj.cos.components.richtext;

import com.vaadin.flow.dom.Style;
import com.wontlost.ckeditor.Config;
import com.wontlost.ckeditor.Constants;
import com.wontlost.ckeditor.VaadinCKEditor;
import com.wontlost.ckeditor.VaadinCKEditorBuilder;
import org.springframework.stereotype.Component;

@Component
public class RichTextField {

    private VaadinCKEditor vaadinCKEditor;

    public void setDefaultLayout() {
        if (this.vaadinCKEditor != null) {
            vaadinCKEditor.getStyle().setOverflow(Style.Overflow.AUTO);
            vaadinCKEditor.getStyle().setBorder("1px solid lightgrey");
        } else {
            vaadinCKEditor = new VaadinCKEditorBuilder().with(txt -> {
                txt.editorType = Constants.EditorType.INLINE;
            }).createVaadinCKEditor();
        }
    }

    public void addDefaultToolBar() {
        if (this.vaadinCKEditor != null) {
            Constants.Toolbar[] toolbar = new Constants.Toolbar[] {
                    Constants.Toolbar.heading,
                    Constants.Toolbar.fontFamily,
                    Constants.Toolbar.fontSize,
                    Constants.Toolbar.fontBackgroundColor,
                    Constants.Toolbar.fontColor,
                    Constants.Toolbar.bold,
                    Constants.Toolbar.italic,
                    Constants.Toolbar.strikethrough,
                    Constants.Toolbar.subscript,
                    Constants.Toolbar.blockQuote,
                    Constants.Toolbar.highlight,
                    Constants.Toolbar.link,
                    Constants.Toolbar.bulletedList,
                    Constants.Toolbar.numberedList,
                    Constants.Toolbar.horizontalLine,
                    Constants.Toolbar.alignment,
                    Constants.Toolbar.findAndReplace,
                    Constants.Toolbar.undo,
                    Constants.Toolbar.redo,
                    Constants.Toolbar.insertTable,
                    Constants.Toolbar.code,
                    Constants.Toolbar.codeBlock,
                    Constants.Toolbar.htmlEmbed};
            Config config = new Config();
            config.setEditorToolBar(toolbar);

            this.vaadinCKEditor.setConfig(config);
        } else {
            vaadinCKEditor = new VaadinCKEditorBuilder().with(txt -> {
                txt.editorType = Constants.EditorType.INLINE;
            }).createVaadinCKEditor();
        }
    }

    public VaadinCKEditor get() {
        if (vaadinCKEditor == null) {
            vaadinCKEditor = new VaadinCKEditorBuilder().with(txt -> {
                txt.editorType = Constants.EditorType.INLINE;
            }).createVaadinCKEditor();
        }

        return vaadinCKEditor;
    }

}
