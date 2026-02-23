import { Component, input, output } from '@angular/core';
import { IconComponent } from '../icon/icon.component';

export interface DockContentItem {
  id: string;
  active?: boolean;
  icon?: string;
  title?: string;
}

@Component({
  selector: 'koku-dock',
  imports: [IconComponent],
  templateUrl: './dock.component.html',
  styleUrl: './dock.component.css',
})
export class DockComponent {
  content = input.required<DockContentItem[]>();
  onActivate = output<DockContentItem>();
}
