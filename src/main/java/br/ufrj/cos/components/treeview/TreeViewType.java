package br.ufrj.cos.components.treeview;

public enum TreeViewType {

        IoTDomain("IoTDomain"),
        ArchitectureSolution("ArchitectureSolution"),
        QualityRequirement("QualityRequirement"),
        Technology("Technology");

        private String displayName;

        TreeViewType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

}
