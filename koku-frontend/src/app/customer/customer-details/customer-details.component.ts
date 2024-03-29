import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild} from '@angular/core';
import {CustomerService} from "../customer.service";
import {NgForm} from "@angular/forms";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
  selector: 'customer-details',
  templateUrl: './customer-details.component.html',
  styleUrls: ['./customer-details.component.scss']
})
export class CustomerDetailsComponent implements OnInit, AfterViewInit {

  @Input('customerId') customerId: number | undefined;
  @Output() afterSaved = new EventEmitter<KokuDto.CustomerDto>();
  @ViewChild('form') ngForm: NgForm | undefined;
  @Input() dialogActions: TemplateRef<any> | null = null;
  @Output() dirty = new EventEmitter<boolean>();

  customer: KokuDto.CustomerDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean = false;

  constructor(public customerService: CustomerService,
              private readonly domSanitizer: DomSanitizer) {
  }

  save(customer: KokuDto.CustomerDto | undefined, form: NgForm) {
    if (form.valid && customer) {
      this.saving = true;
      if (!customer.id) {
        this.customerService.createCustomer(customer).subscribe((response) => {
          this.saving = false;
          this.afterSaved.emit(response);
        }, () => {
          this.saving = false;
        });
      } else {
        this.customerService.updateCustomer(customer).subscribe(() => {
          this.saving = false;
          this.afterSaved.emit(customer);
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  ngOnInit(): void {
    this.createMode = this.customerId === undefined;
    if (this.customerId) {
      this.customerService.getCustomer(this.customerId).subscribe((customer) => {
        this.customer = customer;
        this.loading = false;
      }, () => {
        this.loading = false;
      });
    } else {
      this.customer = {};
      this.loading = false;
    }
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty.emit((this.ngForm || {}).dirty || false);
    });
  }

  convertTelephoneNo(privateTelephoneNo?: string) {
    let result = privateTelephoneNo || '';
    result = result.replace(/\s/g, '').replace('+49', '0');
    result = encodeURIComponent(result);
    return result;
  }

  bypassUri(uri: string) {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(uri);
  }
}
