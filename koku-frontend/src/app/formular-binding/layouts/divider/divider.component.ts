import {booleanAttribute, Component, input} from '@angular/core';

@Component({
  selector: 'divider',
  imports: [],
  templateUrl: './divider.component.html',
  styleUrl: './divider.component.css'
})
export class DividerComponent {

  loading = input(false, {transform: booleanAttribute});
  disabled = input(false, {transform: booleanAttribute});
  text = input<string>()

}
