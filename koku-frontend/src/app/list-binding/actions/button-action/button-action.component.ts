import { booleanAttribute, Component, input, output } from '@angular/core';
import { ListContentSetup, ListItemSetup } from '../../../list/list.component';
import { IconComponent } from '../../../icon/icon.component';

@Component({
  selector: '[http-call-action],http-call-action',
  host: { class: 'flex h-full w-full flex-col overflow-hidden' },
  imports: [IconComponent],
  templateUrl: './button-action.component.html',
})
export class ButtonActionComponent {
  value = input.required<KokuDto.ListViewCallHttpListItemActionDto>();
  register = input.required<ListItemSetup>();
  contentSetup = input.required<ListContentSetup>();
  loading = input(false, { transform: booleanAttribute });

  clicked = output<MouseEvent>();
}
