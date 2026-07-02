import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Navi } from './navi';
import { NaviService } from '../navi.service';
import { IconComponent } from '../../icon/icon.component';

@Component({
  selector: 'koku-navi-entry, [koku-navi-entry]',
  templateUrl: './navi-entry.component.html',
  imports: [RouterLink, RouterLinkActive, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class.gap-2]': "naviEntry().type === 'link'",
    '[class.menu-title]': "naviEntry().type === 'divider'",
    '[class.p-0]': "naviEntry().type === 'divider'",
  },
})
export class NaviEntryComponent {
  naviEntry = input.required<Navi>();
  depth = input<number>(0);
  naviService = inject(NaviService);
}
