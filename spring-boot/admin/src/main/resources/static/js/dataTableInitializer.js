function initializeDataTableAndAddOrdering(columnIndexesWithoutOrderingIcon, isHorizontalScrollBarEnabled){
    $(document).ready(
        function () {
            $('table[class*="default-data-table"]').DataTable({
                columnDefs: [
                    {orderable: false, targets: columnIndexesWithoutOrderingIcon}
                ],
                "scrollX": isHorizontalScrollBarEnabled,
                "sScrollX": "100%",
                "sScrollXInner": "100%"
            });
        }
    );
}