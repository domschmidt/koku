import {
  booleanAttribute,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  input,
  OnDestroy,
  output,
  viewChild,
} from '@angular/core';
import { Event } from '@angular/router';
import { BLANK_A4_PDF, Template } from '@pdfme/common';
import { Designer } from '@pdfme/ui';
import { getFontsData } from '../fonts';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { getPlugins } from '../plugins';
import { deepEqual } from '../../../utils/deepEqual';

@Component({
  selector: 'document-designer-field',
  templateUrl: './document-designer-field.component.html',
  styleUrl: './document-designer-field.component.css',
  imports: [],
  standalone: true,
})
export class DocumentDesignerFieldComponent implements OnDestroy {
  designerRoot = viewChild.required<ElementRef<HTMLDivElement>>('designerRoot');

  value = input.required<string>();
  defaultValue = input<string>();
  name = input.required<string>();

  loading = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });

  onChange = output<string>();
  onBlur = output<Event>();
  onFocus = output<Event>();

  destroyRef = inject(DestroyRef);
  private designer: Designer | undefined;
  private lastSubscribedValue: Template | undefined;

  constructor() {
    toObservable(this.value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.initOrUpdateDesigner(value);
      });
  }

  private initOrUpdateDesigner(rawValue: string) {
    const template: Template = rawValue
      ? JSON.parse(rawValue)
      : {
          basePdf: BLANK_A4_PDF,
          schemas: [],
        };
    this.lastSubscribedValue = template;
    if (!this.designer) {
      this.designer = new Designer({
        domContainer: this.designerRoot().nativeElement,
        template: template,
        options: {
          zoomLevel: 1,
          sidebarOpen: true,
          font: getFontsData(),
          lang: 'de',
          labels: {
            'signature.clear': '🗑️',
          },
          theme: {
            token: { colorPrimary: '#B39DDB' },
          },
          icons: {
            multiVariableText:
              '<svg fill="#000000" width="24px" height="24px" viewBox="0 0 24 24"><path d="M6.643,13.072,17.414,2.3a1.027,1.027,0,0,1,1.452,0L20.7,4.134a1.027,1.027,0,0,1,0,1.452L9.928,16.357,5,18ZM21,20H3a1,1,0,0,0,0,2H21a1,1,0,0,0,0-2Z"/></svg>',
          },
          maxZoom: 250,
        },
        plugins: getPlugins(),
      });
      this.designer.onChangeTemplate((onChangeTemplate) => {
        if (!deepEqual(onChangeTemplate, this.lastSubscribedValue)) {
          this.onChange.emit(JSON.stringify(onChangeTemplate));
        }
      });
    } else {
      if (!deepEqual(template, this.designer.getTemplate())) {
        this.designer.updateTemplate(template);
      }
    }
  }

  ngOnDestroy(): void {
    if (this.designer) {
      this.designer.destroy();
    }
  }
}
