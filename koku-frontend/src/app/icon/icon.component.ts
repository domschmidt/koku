import { Component, input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'koku-icon',
  imports: [NgClass],
  templateUrl: './icon.component.html',
  standalone: true,
})
export class IconComponent {
  name = input.required<string>();
  class = input<string>('size-6');
}
