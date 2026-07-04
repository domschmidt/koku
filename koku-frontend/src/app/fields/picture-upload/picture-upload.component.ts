import { booleanAttribute, Component, inject, input, OnDestroy, output } from '@angular/core';
import { ToastService } from '../../toast/toast.service';
import Compressor from 'compressorjs';

@Component({
  selector: 'picture-upload-field',
  host: { class: 'flex w-full' },
  imports: [],
  templateUrl: './picture-upload.component.html',
})
export class PictureUploadComponent implements OnDestroy {
  private static readonly SUPPORTED_MIME_TYPES: string[] = ['image/png', 'image/jpeg', 'image/gif'];
  private currentCompressor: Compressor | undefined;

  toastService = inject(ToastService);

  config = input<KokuDto.PictureUploadFormularField>();

  value = input.required<string>();
  defaultValue = input<string>('');
  name = input.required<string>();
  label = input<string>();
  compressMaxResolution = input<[number, number]>([256, 256]);
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  loading = input(false, { transform: booleanAttribute });
  changed = output<string>();

  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      if (PictureUploadComponent.SUPPORTED_MIME_TYPES.includes(file.type)) {
        this.currentCompressor?.abort();
        this.currentCompressor = new Compressor(file, {
          quality: 0.6,
          maxWidth: (this.compressMaxResolution() || [])[0],
          maxHeight: (this.compressMaxResolution() || [])[1],
          success: (result) => {
            this.currentCompressor = undefined;
            const reader = new FileReader();
            reader.onload = (e) => {
              const result = e.target?.result as string;
              this.changed.emit(result);
            };
            reader.readAsDataURL(result);
          },
          error: () => {
            this.currentCompressor = undefined;
            this.toastService.add('Fehler bei der Bildbearbeitung', 'error');
          },
        });
      } else {
        this.toastService.add('Bildformat wird nicht unterstützt.', 'error');
      }
    }
  }

  clearImage() {
    this.changed.emit('');
  }

  ngOnDestroy(): void {
    this.currentCompressor?.abort();
  }
}
