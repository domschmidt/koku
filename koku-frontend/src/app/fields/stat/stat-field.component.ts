import {booleanAttribute, Component, input, output} from '@angular/core';
import {IconComponent} from '../../icon/icon.component';

@Component({
  selector: 'stat-field',
  templateUrl: './stat-field.component.html',
  styleUrl: './stat-field.component.css',
  imports: [
    IconComponent
  ],
  standalone: true
})
export class StatFieldComponent {

  value = input.required<string>();
  defaultValue = input<string>();
  title = input<string>();
  description = input<string>();
  icon = input<string>();
  name = input.required<string>();
  loading = input(false, {transform: booleanAttribute});
  readonly = input(false, {transform: booleanAttribute});
  required = input(false, {transform: booleanAttribute});
  disabled = input(false, {transform: booleanAttribute});
  onBlur = output<void>();
  onFocus = output<void>();

}
