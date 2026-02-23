import { booleanAttribute, Component, inject, input, output } from '@angular/core';
import { ToastService } from '../../toast/toast.service';
import Compressor from 'compressorjs';

@Component({
  selector: 'picture-upload-field',
  imports: [],
  templateUrl: './picture-upload.component.html',
  styleUrl: './picture-upload.component.css',
})
export class PictureUploadComponent {
  private static readonly SUPPORTED_MIME_TYPES: string[] = ['image/png', 'image/jpeg', 'image/gif'];

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
  onChange = output<string>();

  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      if (PictureUploadComponent.SUPPORTED_MIME_TYPES.indexOf(file.type) < 0) {
        this.toastService.add('Bildformat wird nicht unterstützt.', 'error');
      } else {
        new Compressor(file, {
          quality: 0.6,
          maxWidth: (this.compressMaxResolution() || [])[0],
          maxHeight: (this.compressMaxResolution() || [])[1],
          success: (result) => {
            const reader = new FileReader();
            reader.onload = (e) => {
              const result = e.target?.result as string;
              this.onChange.emit(result);
            };
            reader.readAsDataURL(result);
          },
          error: () => {
            this.toastService.add('Fehler bei der Bildbearbeitung', 'error');
          },
        });
      }
    }
  }

  clearImage() {
    this.onChange.emit('');
  }
}
