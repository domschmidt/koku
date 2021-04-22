import {Injectable} from '@angular/core';
import {AlertDialogButtonConfig, AlertDialogComponent, AlertDialogData} from "../alert-dialog/alert-dialog.component";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";

@Injectable({
  providedIn: 'root'
})
export class PreventLosingChangesService {

  constructor(private readonly dialog: MatDialog) {
  }

  preventLosingChanges(dirty: boolean, abortCB: () => void, discardCB?: () => void) {
    if (dirty) {
      const dialogData: AlertDialogData = {
        headline: 'Ungespeicherte Änderungen',
        message: `Wollen Sie ungespeicherte Änderungen verwerfen?`,
        buttons: [{
          text: 'Abbrechen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            dialogRef.close();
            if (discardCB) {
              discardCB();
            }
          }
        }, {
          text: 'Verwerfen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            dialogRef.close();
            abortCB();
          }
        }]
      };
      this.dialog.open(AlertDialogComponent, {
        data: dialogData,
        width: '100%',
        maxWidth: 700,
        closeOnNavigation: false,
        position: {
          top: '20px'
        }
      });
    } else {
      abortCB();
    }
  }

}
