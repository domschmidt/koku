import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'koku-toast',
  imports: [],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css',
})
export class ToastComponent {
  readonly toastService = inject(ToastService);
}
