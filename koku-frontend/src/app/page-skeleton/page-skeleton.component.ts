import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { NaviListComponent } from '../navi/navi-list/navi-list.component';
import { NaviService } from '../navi/navi.service';

@Component({
  selector: 'koku-page-skeleton',
  templateUrl: './page-skeleton.component.html',
  imports: [RouterOutlet, NaviListComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageSkeletonComponent {
  activatedRoute = inject(ActivatedRoute);
  naviService = inject(NaviService);

  onChange($event: Event) {
    if ($event.target) {
      const checked = ($event.target as HTMLInputElement).checked;
      if (checked) {
        this.naviService.open();
      } else {
        this.naviService.close();
      }
    }
  }
}
