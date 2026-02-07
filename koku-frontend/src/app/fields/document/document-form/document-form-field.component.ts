import {
  AfterViewInit,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  input,
  OnChanges,
  OnDestroy,
  output,
  signal,
  SimpleChanges,
  viewChild
} from '@angular/core';
import {Template, ZOOM} from '@pdfme/common';
import {Form} from '@pdfme/ui';
import {getFontsData} from '../fonts';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {HttpClient} from '@angular/common/http';
import {getPlugins} from '../plugins';
import {get} from '../../../utils/get';
import {OutletDirective} from '../../../portal/outlet.directive';
import {PortalDirective} from '../../../portal/portal.directive';
import {generate} from '@pdfme/generator';
import {IconComponent} from '../../../icon/icon.component';
import {ToastService} from '../../../toast/toast.service';
import dayjs from 'dayjs';
import {FullscreenService} from '../../../fullscreen/fullscreen.service';
import {fromEvent} from 'rxjs';
import {debounce} from '../../../utils/debounce';

@Component({
  selector: 'document-form-field',
  templateUrl: './document-form-field.component.html',
  styleUrl: './document-form-field.component.css',
  imports: [
    PortalDirective,
    IconComponent
  ],
  standalone: true
})
export class DocumentFormFieldComponent implements OnDestroy, OnChanges, AfterViewInit {

  formRoot = viewChild.required<ElementRef<HTMLDivElement>>("formRoot");

  documentUrl = input.required<string>();
  submitUrl = input.required<string>();
  submitMethod = input<'POST' | 'PUT'>();
  context = input<{ [key: string]: any }>();
  buttonDockOutlet = input<OutletDirective>();

  destroyRef = inject(DestroyRef);
  httpClient = inject(HttpClient);
  toastService = inject(ToastService);
  fullscreenService = inject(FullscreenService);

  submitting = signal<boolean>(false);

  onSubmit = output<any>();

  private form: Form | undefined;
  private document: KokuDto.KokuDocumentDto | undefined;
  private documentMeta: { document: { id: string } } | undefined;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['documentUrl']) {
      this.httpClient.get<KokuDto.KokuDocumentDto>(this.documentUrl())
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((document) => {
          this.documentMeta = {
            document: {
              id: 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
                const r = (Math.random() * 16) | 0;
                const v = c == 'x' ? r : (r & 0x3) | 0x8;
                return v.toString(16);
              })
            }
          };
          this.document = document;
          this.initOrUpdateForm();
        });
    }
  }

  private initOrUpdateForm() {
    const now = Date.now();
    const utils = {
      utils: {
        today: dayjs(now).format("YYYY-MM-DDTHH:mm:ss"),
        'today+1y': dayjs(now).add(1, 'year').format("YYYY-MM-DDTHH:mm:ss")
      }
    };
    if (!this.document || !this.document.template) {
      throw new Error('Missing template in document');
    }
    const inputs: Record<string, any>[] = [];
    const parsedTemplate = JSON.parse(this.document.template) as Template;
    for (const currentTemplateSchema of (parsedTemplate.schemas)) {
      const currentSchemaContents: Record<string, any> = {};
      for (const currentTemplateSchemaContent of (currentTemplateSchema)) {
        const contextSnapshot = this.context();
        switch (currentTemplateSchemaContent['type']) {
          case "text": {
            currentSchemaContents[currentTemplateSchemaContent.name] = (currentTemplateSchemaContent.content || '').replace(/\{([\w\.\s]+)\}/g, (_, key) => {
              return get({
                ...contextSnapshot,
                ...utils,
                ...(this.documentMeta || {}),
              }, (key || '').trim(), `{${key}}`);
            });
            break;
          }
          case "date": {
            currentSchemaContents[currentTemplateSchemaContent.name] = (currentTemplateSchemaContent.name || '').replace(/\{([\w\.\s\+]+)\}/g, (_, key) => {
              return get({
                ...contextSnapshot,
                ...utils,
                ...(this.documentMeta || {}),
              }, (key || '').trim(), `{${key}}`);
            });
            break;
          }
          case "multiVariableText": {
            const values: { [key: string]: string } = {};
            for (const currentVariable of currentTemplateSchemaContent['variables'] as string[] || []) {
              values[currentVariable] = get({
                ...contextSnapshot,
                ...utils,
                ...(this.documentMeta || {}),
              }, currentVariable.trim(), `{${currentVariable}}`);
            }

            currentSchemaContents[currentTemplateSchemaContent.name] = JSON.stringify(values);
            break;
          }
          case "qrcode": {
            currentSchemaContents[currentTemplateSchemaContent.name] = (currentTemplateSchemaContent.content || '').replace(/\{([\w\.\s]+)\}/g, (_, key) => {
              return get({
                ...contextSnapshot,
                ...utils,
                ...(this.documentMeta || {}),
              }, (key || '').trim(), `{${key}}`);
            });
            break;
          }
        }
      }
      inputs.push(currentSchemaContents);
    }

    const form = this.form;
    if (!form) {
      this.form = new Form({
        domContainer: this.formRoot().nativeElement,
        template: parsedTemplate,
        inputs,
        options: {
          font: getFontsData(),
          lang: "de",
          labels: {
            'signature.clear': "🗑️",
          },
          theme: {
            token: {colorPrimary: "#25c2a0"},
          },
          icons: {
            multiVariableText:
              '<svg fill="#000000" width="24px" height="24px" viewBox="0 0 24 24"><path d="M6.643,13.072,17.414,2.3a1.027,1.027,0,0,1,1.452,0L20.7,4.134a1.027,1.027,0,0,1,0,1.452L9.928,16.357,5,18ZM21,20H3a1,1,0,0,0,0,2H21a1,1,0,0,0,0-2Z"/></svg>',
          },
          maxZoom: 10000,
        },
        plugins: getPlugins()
      });
      this.form.resizeObserver.disconnect();
      this.form.resizeObserver = new ResizeObserver(debounce(() => {
        const self = this.form as any;

        const rect = self.domContainer.getBoundingClientRect();

        const pageWidth = self.template.basePdf.width;
        const pageHeight = self.template.basePdf.height;
        const aspectRatio = pageHeight / pageWidth;

        const calculatedWidth = Math.floor(rect.width);
        self.size = {
          height: Math.max(
            rect.height,
            Math.min(calculatedWidth * aspectRatio, pageHeight * ZOOM)
          ),
          width: calculatedWidth
        };
        self.render();
      }, 50));
      this.form.resizeObserver.observe(this.formRoot().nativeElement);
    } else {
      form.updateTemplate(parsedTemplate);
      form.setInputs(inputs);
    }
  }

  ngOnDestroy(): void {
    if (this.form) {
      this.form.destroy();
    }
    this.fullscreenService.exit();
  }

  submit() {
    const submitUrlSnapshot = this.submitUrl();
    const formSnapshot = this.form;
    if (submitUrlSnapshot && formSnapshot) {
      this.submitting.set(true);

      let missingFieldValueFound = false;
      this.form?.getTemplate().schemas.forEach((schemaPage) => schemaPage.forEach((schema) => {
        if (schema.required && !schema.readOnly && !this.form?.getInputs().some((input) => input[schema.name])) {
          missingFieldValueFound = true;
          this.toastService.add('Überprüfe die Eingaben', 'warning');
        }
      }));
      if (!missingFieldValueFound) {
        generate({
          template: formSnapshot.getTemplate(),
          inputs: formSnapshot.getInputs(),
          plugins: getPlugins(),
          options: {
            title: this.document?.name,
            author: 'KoKu',
            lang: 'de',
            creator: 'KoKu',
          }
        }).then((pdf) => {
          if (!this.document || !this.document.name) {
            throw new Error('Missing name in document');
          }
          const formData = new FormData();
          formData.append('file', new Blob([new Uint8Array(pdf.buffer)], {type: 'application/pdf'}), this.document.name + '.pdf');
          this.httpClient.request(
            this.submitMethod() || 'POST',
            `${submitUrlSnapshot}${submitUrlSnapshot.includes('?') ? '&' : '?'}id=${this.documentMeta?.document.id}`,
            {
              body: formData,
            }
          ).subscribe((payload) => {
            this.submitting.set(false);
            this.onSubmit.emit(payload);
            this.toastService.add("Dokument erstellt");
          }, () => {
            this.submitting.set(false);
            this.toastService.add("Fehler beim Speichern", 'error');
          })
        }).catch((error) => {
          this.toastService.add('Fehler beim Erstellen der PDF', 'error');
          this.submitting.set(false);
        });
      } else {
        this.submitting.set(false);
      }
    }
  }

  enterFullscreen() {
    this.fullscreenService.enter(this.formRoot().nativeElement);
  }

  exitFullscreen() {
    this.fullscreenService.exit();
  }

  ngAfterViewInit() {
    fromEvent(screen.orientation, 'change')
      .pipe(
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        if (screen.orientation.type.includes('landscape')) {
          this.enterFullscreen();
        } else {
          this.exitFullscreen();
        }
      });

  }

}
