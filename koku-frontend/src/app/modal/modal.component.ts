import { Component, inject } from '@angular/core';
import { ModalService } from './modal.service';
import { ButtonComponent } from '../button/button.component';
import { ModalType } from './modal.type';
import { ModalContentRendererComponent } from './modal-content-renderer.component';

@Component({
  selector: 'koku-modal',
  host: { class: 'relative z-[999]' },
  imports: [ButtonComponent, ModalContentRendererComponent],
  templateUrl: './modal.component.html',
})
export class ModalComponent {
  readonly modalService = inject(ModalService);

  handleBackdropClick(event: Event, modal: ModalType) {
    if (event.target === event.currentTarget) {
      if (modal.clickOutside) {
        modal.clickOutside(event);
      }
    }
  }
}
