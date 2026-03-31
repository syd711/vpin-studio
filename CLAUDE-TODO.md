## Task 

Fix the sorting in the TableOverviewColumnSorter. Check also if the way columnBackupDate is applied is feasible.

      else if (column.equals(tableOverviewController.columnBackupDate)) {
        comp = (o1, o2) -> {
          if (o1.backupDate == null || o2.backupDate == null) {
            return -1;
          }
          else {
            return TableOverviewController.dateTimeFormat.format(o1.backupDate).compareTo(TableOverviewController.dateTimeFormat.format(o2.backupDate));
          }
        };
      }

## Task 

Read the file "C:\Users\matth\Downloads\Zen Games.html". Check if the "B2S Name" column names (b2s is equal directb2s files) 
values match with the ones in the different *_mapping.json files in "resources/pupgames". 
Fixed the the .directb2s names when you find the matching tables. If you find a a table name in the html files that is not in 
any of the *_mapping.json files, add the missing entries to the fx3_b2s_mapping and fx_b2s_mapping file.


