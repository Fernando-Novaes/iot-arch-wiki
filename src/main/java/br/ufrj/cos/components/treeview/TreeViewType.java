package br.ufrj.cos.components.treeview;

public enum TreeViewType {

        IoTDomain("IoT Domain"),
        ArchitectureSolution("Architecture Solution"),
        QualityRequirement("Quality Requirement"),
        Technology("Technology"),

        IoTDomain_Filtered("IoT Domain Filtered"),
        ArchitectureSolution_Filtered("Architecture Solution Filtered"),
        QualityRequirement_Filtered("Quality Requirement Filtered"),
        Technology_Filtered("Technology Filtered"),

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
