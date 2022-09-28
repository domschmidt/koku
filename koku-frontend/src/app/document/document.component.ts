import {Component} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {DocumentService} from "./document.service";
import {DocumentDetailsComponentData, DocumentDialogComponent} from "./document-dialog.component";

@Component({
  selector: 'document',
  templateUrl: './document.component.html',
  styleUrls: ['./document.component.scss']
})
export class DocumentComponent {

  documents$: Observable<KokuDto.FormularDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public documentService: DocumentService) {
    this.documents$ = this.documentService.getDocuments();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.documentService.getDocuments(debouncedValue));
  }

  openDocumentDetails(document: KokuDto.FormularDto) {
    const dialogData: DocumentDetailsComponentData = {
      documentId: document.id || 0
    };
    this.dialog.open(DocumentDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      width: '100%',
      maxWidth: 1280,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.FormularDto) {
    return item.id;
  }

  addNewDocument() {
    const dialogData: DocumentDetailsComponentData = {};
    this.dialog.open(DocumentDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      width: '100%',
      maxWidth: 1280,
      position: {
        top: '20px'
      }
    });
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }

}
