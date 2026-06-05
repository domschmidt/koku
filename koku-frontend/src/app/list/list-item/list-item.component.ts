import { Component, input } from '@angular/core';
import { ListContentSetup, ListItemSetup } from '../list.component';
import { ListFieldRendererComponent } from './list-field-renderer.component';

export type ListFieldEvent = 'onClick' | 'onChange' | 'onInput' | 'onFocus' | 'onBlur' | 'onInit';
@Component({
  selector: '[list-item],list-item',
  imports: [ListFieldRendererComponent],
  templateUrl: './list-item.component.html',
})
export class ListItemComponent {
  register = input.required<ListItemSetup>();

  contentSetup = input.required<ListContentSetup>();
}
