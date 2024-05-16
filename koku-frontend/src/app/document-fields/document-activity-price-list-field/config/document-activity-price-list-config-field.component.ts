import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {DocumentService} from '../../../document/document.service';
import {
  DocumentFieldConfigurationTypes,
  DocumentFieldMeta
} from '../../../document-designer-module/document-field-config';
import {MatSelectChange} from '@angular/material/select';
import {DocumentTextConfigFieldComponent} from '../../document-text-field/config/document-text-config-field.component';
import {DocumentTextFieldComponent} from '../../document-text-field/document-text-field.component';
import {
  SortableDialogButtonConfig,
  SortableDialogComponent,
  SortableDialogData,
  SortableDialogItem
} from '../../../sortable-dialog/sortable-dialog.component';
import {HttpClient, HttpParams} from '@angular/common/http';

@Component({
  selector: 'document-price-list-config-field',
  templateUrl: './document-activity-price-list-config-field.component.html',
  styleUrls: ['./document-activity-price-list-config-field.component.scss']
})
export class DocumentActivityPriceListConfigFieldComponent {

  activityPriceListField: KokuDto.ActivityPriceListFormularItemDto | undefined;
  activityPriceListItemRowFieldConfig: DocumentFieldConfigurationTypes = {};
  activityPriceListGroupRowFieldConfig: DocumentFieldConfigurationTypes | null = null; // ergibt sich aus dem group by identifier
  saving = false;
  loading = true;
  createMode: boolean;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: {
      field?: KokuDto.ActivityPriceListFormularItemDto
      meta: DocumentFieldMeta
    },
    public dialogRef: MatDialogRef<DocumentActivityPriceListConfigFieldComponent>,
    public dialog: MatDialog,
    public documentService: DocumentService,
    public httpClient: HttpClient
  ) {
    this.createMode = data.field === undefined;
    if (data.field === undefined) {
      this.activityPriceListField = {
        id: 0,
        ['@type']: 'ActivityPriceListFormularItemDto',
        itemRows: [],
        groupRows: []
      };
    } else {
      this.activityPriceListField = {...data.field};
    }
    this.activityPriceListItemRowFieldConfig = data.meta.fieldConfig;

    if (((this.activityPriceListField || {}).groupBy || '') === 'CATEGORY') {
      this.documentService.getDocumentActivityPriceListGroupTextReplacementToken('NONE').subscribe((result) => {
        this.activityPriceListGroupRowFieldConfig = {
          TextFormularItemDto: {
            component: DocumentTextConfigFieldComponent,
            meta: {
              replacementTokens: result
            },
            renderComponent: DocumentTextFieldComponent,
            name: 'Text'
          }
        };
      });
    }
  }

  save(form: NgForm): void {
    if (form.valid) {
      this.dialogRef.close(this.activityPriceListField);
    }
  }

  changeGroupBy($event: MatSelectChange): void {
    if (this.activityPriceListField) {
      this.activityPriceListField.groupBy = $event.value;
    }

    this.activityPriceListGroupRowFieldConfig = null;

    if (((this.activityPriceListField || {}).groupBy || '') === 'CATEGORY') {
      this.documentService.getDocumentActivityPriceListGroupTextReplacementToken('NONE').subscribe((result) => {
        this.activityPriceListGroupRowFieldConfig = {
          TextFormularItemDto: {
            component: DocumentTextConfigFieldComponent,
            meta: {
              replacementTokens: result
            },
            renderComponent: DocumentTextFieldComponent,
            name: 'Text'
          }
        };
      });
    }
  }

  openItemSort(): void {
    this.httpClient.get<KokuDto.ActivityDto[]>('/api/activities', {
      params: new HttpParams({
        fromObject: {
          search: '',
          havingRelevanceForPriceListOnly: true
        }
      })
    }).subscribe((result) => {
      if (this.activityPriceListField) {
        const sortableItems: SortableDialogItem[] = [];
        if (this.activityPriceListField.groupBy === 'CATEGORY') {
          const knownCategories: { [key: string]: SortableDialogItem } = {};
          for (const activityDto of result) {
            if (activityDto.category && activityDto.category.id) {
              if (knownCategories[activityDto.category.id]) {
                (knownCategories[activityDto.category.id].items || []).push({
                  ...activityDto,
                  id: activityDto.id || 0,
                  description: activityDto.description || ''
                });
              } else {
                knownCategories[activityDto.category.id] = {
                  ...activityDto.category,
                  id: activityDto.category.id || 0,
                  description: activityDto.category.description || '',
                  items: [{
                    ...activityDto,
                    id: activityDto.id || 0,
                    description: activityDto.description || ''
                  }]
                };
              }
            } else {
              if (knownCategories['']) {
                (knownCategories[''].items || []).push({
                  ...activityDto,
                  id: activityDto.id || 0,
                  description: activityDto.description || ''
                });
              } else {
                knownCategories[''] = {
                  ...activityDto.category,
                  description: 'Ohne Kategorie',
                  id: 0,
                  items: [{
                    ...activityDto,
                    id: activityDto.id || 0,
                    description: activityDto.description || ''
                  }]
                };
              }
            }
          }
          for (const currentKnownCategory of Object.keys(knownCategories)) {
            sortableItems.push(knownCategories[currentKnownCategory]);
          }
        } else {
          for (const activityDto of result) {
            sortableItems.push({
              ...activityDto,
              id: activityDto.id || 0,
              description: activityDto.description || ''
            });
          }
        }

        this.dialog.open<SortableDialogComponent, SortableDialogData>(SortableDialogComponent, {
          data: {
            headline: 'Sortierung bearbeiten',
            items: sortableItems,
            buttons: [{
              text: 'Verwerfen',
              onClick: (mouseEvent: Event, button: SortableDialogButtonConfig, dialogRef: MatDialogRef<SortableDialogComponent>) => {
                dialogRef.close();
              }
            }, {
              text: 'Übernehmen',
              onClick: (mouseEvent: Event, button: SortableDialogButtonConfig, dialogRef: MatDialogRef<SortableDialogComponent>) => {
                if (this.activityPriceListField) {
                  this.activityPriceListField.sortByIds = this.buildSortedList(sortableItems);
                }
                dialogRef.close();
              }
            }]
          },
          width: '100%',
          maxWidth: 700,
          closeOnNavigation: false,
          position: {
            top: '20px'
          }
        });

      }
    });
  }

  private buildSortedList(sortableItems: SortableDialogItem[]): number[] {
    let sort: number[] = [];
    for (const currentSortableItem of sortableItems) {
      if (currentSortableItem.id !== undefined) {
        sort.push(currentSortableItem.id);
        if (currentSortableItem.items) {
          sort = [
            ...sort,
            ...this.buildSortedList(currentSortableItem.items)
          ];
        }
      }
    }
    return sort;
  }
}
