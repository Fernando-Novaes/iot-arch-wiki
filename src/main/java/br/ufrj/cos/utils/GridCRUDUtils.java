package br.ufrj.cos.utils;

import br.ufrj.cos.domain.DomainBase;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import org.vaadin.crudui.crud.impl.GridCrud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GridCRUDUtils {

    /***
     * Creates the filter for the GridCRUD
     * @param gridCRUD the Grid to insert the filter
     * @param filterText Text to be shown in the filter
     * @param textFieldSize the ize of the filter field
     * @return TextField
     */
    public static TextField createGridTextFilter(GridCrud<?> gridCRUD, String filterText, String textFieldSize) {
        TextField filter = new TextField();
        filter.setPlaceholder(filterText);
        filter.setClearButtonVisible(true);
        filter.setMinWidth(textFieldSize);
        filter.addValueChangeListener(e -> gridCRUD.refreshGrid());
        gridCRUD.getCrudLayout().addFilterComponent(filter);

        return filter;
    }

    /***
     *
     * @param gridCrud
     * @param label
     * @param width
     * @param items
     * @return
     * @param <DomainBase>
     */
    public static  <DomainBase> ComboBox<?> createGridComboFilter(GridCrud<?> gridCrud, String label, String width, List<DomainBase> items) {
        ComboBox<DomainBase> comboBox = new ComboBox<>();
        comboBox.setWidth(width);
        comboBox.setPlaceholder(label);
        comboBox.setItems(items);
        comboBox.setClearButtonVisible(true);
        gridCrud.getCrudLayout().addFilterComponent(comboBox);
        comboBox.addValueChangeListener(e -> gridCrud.refreshGrid());

        return comboBox;
    }

    public static void setColumnsOrder(GridCrud gridCRUD, String... columnsNames) {
        List<Grid.Column<?>> columns = new ArrayList<>();
        Arrays.stream(columnsNames).forEach(c -> {
            columns.add(gridCRUD.getGrid().getColumnByKey(c));
        });

        gridCRUD.getGrid().setColumnOrder(columns);
    }

}
