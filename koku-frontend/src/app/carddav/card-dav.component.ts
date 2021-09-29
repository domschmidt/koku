import {Component, OnInit} from '@angular/core';
import {CardDavService} from "./card-dav.service";

@Component({
  selector: 'carddav',
  templateUrl: './card-dav.component.html',
  styleUrls: ['./card-dav.component.scss']
})
export class CardDavComponent implements OnInit {
  public syncing: boolean = false;
  public loading: boolean = false;
  info: KokuDto.CardDavInfoDto | null = null;

  constructor(private readonly carddavService: CardDavService) {
  }

  ngOnInit(): void {
    this.loading = true;
    this.carddavService.getInfo().subscribe((result) => {
      this.info = result;
      this.loading = false;
    });
  }

  sync() {
    this.syncing = true;
    this.carddavService.sync().subscribe(() => {
      this.syncing = false;
    }, () => {
      this.syncing = false;
    });
  }

}
