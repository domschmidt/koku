import {booleanAttribute, Component, input, output} from '@angular/core';
import {ListContentSetup, ListItemSetup} from '../../../list/list.component';
import {IconComponent} from '../../../icon/icon.component';


@Component({
  selector: '[http-call-action],http-call-action',
  imports: [
    IconComponent
  ],
  templateUrl: './button-action.component.html',
  styleUrl: './button-action.component.css'
})
export class ButtonActionComponent {

  value = input.required<KokuDto.ListViewCallHttpListItemActionDto>();
  register = input.required<ListItemSetup>();
  contentSetup = input.required<ListContentSetup>();
  loading = input(false, {transform: booleanAttribute});

  onClick = output<MouseEvent>();

}
