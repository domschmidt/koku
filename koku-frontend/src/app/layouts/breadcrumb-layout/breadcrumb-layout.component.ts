import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {NaviService} from "../../navi/navi.service";

@Component({
  selector: 'breadcrumb-layout',
  templateUrl: './breadcrumb-layout.component.html',
  styleUrls: ['./breadcrumb-layout.component.scss']
})
export class BreadcrumbLayoutComponent implements AfterViewInit {

  constructor(public readonly activatedRoute: ActivatedRoute,
              public readonly router: Router,
              public readonly naviService: NaviService) {
  }

  ngOnInit(): void {
    this.router.getCurrentNavigation();
  }

  ngAfterViewInit(): void {
    this.router.getCurrentNavigation();
  }

}
