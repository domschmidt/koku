import {Component} from '@angular/core';
import {ActivatedRoute, RouterOutlet} from "@angular/router";
import {NaviService} from "../../navi/navi.service";
import {slideInAnimation} from "../../animations";

@Component({
  selector: 'breadcrumb-layout',
  templateUrl: './breadcrumb-layout.component.html',
  styleUrls: ['./breadcrumb-layout.component.scss']
})
export class BreadcrumbLayoutComponent {

  constructor(public readonly activatedRoute: ActivatedRoute,
              public readonly naviService: NaviService) {
  }

}
