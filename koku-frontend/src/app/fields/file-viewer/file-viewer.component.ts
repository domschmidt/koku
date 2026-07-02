import {
  Component,
  DestroyRef,
  ElementRef,
  Renderer2,
  effect,
  inject,
  input,
  OnChanges,
  OnDestroy,
  signal,
  SimpleChanges,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { get } from '../../utils/get';
import { OutletDirective } from '../../portal/outlet.directive';
import { PortalDirective } from '../../portal/portal.directive';
import { IconComponent } from '../../icon/icon.component';

@Component({
  selector: 'file-viewer',
  templateUrl: './file-viewer.component.html',
  styleUrl: './file-viewer.component.css',
  imports: [PortalDirective, IconComponent],
  standalone: true,
})
export class FileViewerComponent implements OnChanges, OnDestroy {
  sourceUrl = input.required<string>();
  fileUrl = input.required<string>();
  mimeTypeSourcePath = input.required<string>();
  buttonDockOutlet = input<OutletDirective>();

  destroyRef = inject(DestroyRef);
  httpClient = inject(HttpClient);
  private readonly renderer = inject(Renderer2);

  mimeType = signal<string | null>(null);
  fileBlobUrl = signal<string | null>(null);
  source = signal<unknown>(null);
  private readonly pdfPreview = viewChild<ElementRef<HTMLObjectElement>>('pdfPreview');
  private objectUrl: string | null = null;

  constructor() {
    effect(() => {
      const pdfPreview = this.pdfPreview();
      if (!pdfPreview) {
        return;
      }

      const fileBlobUrl = this.fileBlobUrl();
      if (fileBlobUrl) {
        this.renderer.setAttribute(pdfPreview.nativeElement, 'data', fileBlobUrl);
      } else {
        this.renderer.removeAttribute(pdfPreview.nativeElement, 'data');
      }
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['sourceUrl'] || changes['fileUrl'] || changes['mimeTypeSourcePath']) {
      this.mimeType.set(null);
      this.clearBlobUrl();
      this.source.set(null);
      this.httpClient
        .get<unknown>(this.sourceUrl())
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((source) => {
          this.source.set(source);
          this.mimeType.set(get(source, this.mimeTypeSourcePath()));

          this.httpClient
            .get(this.fileUrl(), { responseType: 'blob' })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((blob) => {
              this.setBlobUrl(URL.createObjectURL(blob));
            });
        });
    }
  }

  ngOnDestroy(): void {
    this.clearBlobUrl();
  }

  private setBlobUrl(objectUrl: string): void {
    this.clearBlobUrl();
    this.objectUrl = objectUrl;
    this.fileBlobUrl.set(`${objectUrl}#toolbar=0&view=FitH`);
  }

  private clearBlobUrl(): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
    this.fileBlobUrl.set(null);
  }
}
