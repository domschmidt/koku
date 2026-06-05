import { Component, DestroyRef, inject, input, output } from '@angular/core';
import { ListContentSetup, ListInlineItem, ListItemSetup } from '../list.component';
import { HttpClient } from '@angular/common/http';
import { ModalService } from '../../modal/modal.service';
import { ToastService } from '../../toast/toast.service';
import { UNIQUE_REF_GENERATOR } from '../../utils/uniqueRef';
import { ListActionRendererComponent } from './list-action-renderer.component';

@Component({
  selector: '[list-item-action],list-item-action',
  imports: [ListActionRendererComponent],
  templateUrl: './list-item-action.component.html',
})
export class ListItemActionComponent {
  destroyRef = inject(DestroyRef);
  httpClient = inject(HttpClient);
  modalService = inject(ModalService);
  toastService = inject(ToastService);

  register = input.required<ListItemSetup>();
  listRegister = input.required<ListItemSetup[]>();
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<Record<string, string> | null>(null);

  componentRef = UNIQUE_REF_GENERATOR.generate();

  openInlineContentRequested = output<ListInlineItem>();
  openRoutedContentRequested = output<string[]>();
  reloadRequested = output<void>();
}
