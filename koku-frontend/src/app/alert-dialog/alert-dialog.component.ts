import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

export interface AlertDialogButtonConfig {
  text: string;
  loading?: boolean;
  onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => void;
}

export interface AlertDialogData {
  headline?: string;
  message: string;
  buttons?: AlertDialogButtonConfig[];
}

@Component({
  selector: 'alert-dialog',
  templateUrl: './alert-dialog.component.html'
})
export class AlertDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: AlertDialogData,
              public dialogRef: MatDialogRef<AlertDialogComponent>) {
  }
}
