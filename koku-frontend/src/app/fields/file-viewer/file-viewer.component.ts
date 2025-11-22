import {Component, DestroyRef, inject, input, OnChanges, signal, SimpleChanges} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {HttpClient} from '@angular/common/http';
import {get} from '../../utils/get';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {OutletDirective} from '../../portal/outlet.directive';
import {PortalDirective} from '../../portal/portal.directive';
import {IconComponent} from '../../icon/icon.component';

@Component({
  selector: 'file-viewer',
  templateUrl: './file-viewer.component.html',
  styleUrl: './file-viewer.component.css',
  imports: [
    PortalDirective,
    IconComponent
  ],
  standalone: true
})
export class FileViewerComponent implements OnChanges {

  sourceUrl = input.required<string>();
  fileUrl = input.required<string>();
  mimeTypeSourcePath = input.required<string>();
  buttonDockOutlet = input<OutletDirective>();

  destroyRef = inject(DestroyRef);
  httpClient = inject(HttpClient);
  domSanitizer = inject(DomSanitizer);

  mimeType = signal<string | null>(null);
  fileBlobUrl = signal<SafeResourceUrl | null>(null);
  source = signal<any | null>(null);

  ngOnChanges(changes: SimpleChanges) {
    if (changes['sourceUrl'] || changes['fileUrl'] || changes['mimeTypeSourcePath']) {
      this.mimeType.set(null);
      this.fileBlobUrl.set(null);
      this.source.set(null);
      this.httpClient.get<any>(this.sourceUrl())
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((source) => {
          this.source.set(source);
          this.mimeType.set(get(source, this.mimeTypeSourcePath()));

          this.httpClient.get(this.fileUrl(), { responseType: 'blob' })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((blob) => {
              this.fileBlobUrl.set(this.domSanitizer.bypassSecurityTrustResourceUrl(URL.createObjectURL(blob) + "#toolbar=0&view=FitH"));
            });
        });
    }
  }

}
