package br.ufrj.cos.components.treeview;

public enum TreeViewType {

        IoTDomain("IoT Domain"),
        ArchitectureSolution("Architecture Solution"),
        QualityRequirement("Quality Requirement"),
        Technology("Technology"),
        Filtered("Filtered");

        private final String displayName;

        TreeViewType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

}
