import {Component} from '@angular/core';
import {DomSanitizer} from "@angular/platform-browser";
import {MatIconRegistry} from "@angular/material/icon";


@Component({
  selector: 'root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {

  constructor(
    private matIconRegistry: MatIconRegistry,
    private readonly domSanitizer: DomSanitizer
  ) {

    this.matIconRegistry.addSvgIcon(
      'voip',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/voip.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_before',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_before.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_after',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_after.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_above',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_above.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_below',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_below.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'brick_block_add',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/brick_block_add.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'deposit',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/deposit.svg')
    );
  }

}
