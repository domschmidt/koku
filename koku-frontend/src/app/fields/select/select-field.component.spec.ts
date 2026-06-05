import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SelectFieldComponent } from './select-field.component';

@Component({
  imports: [SelectFieldComponent],
  template: `
    <select-field name="customerId" value="41" [disabled]="disabled()">
      <ng-container ngProjectAs="append-outer">
        <button id="create-customer" type="button">Kunde anlegen</button>
      </ng-container>
    </select-field>
  `,
})
class SelectFieldHostComponent {
  disabled = signal(false);
}

describe('SelectFieldComponent', () => {
  let fixture: ComponentFixture<SelectFieldHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [SelectFieldHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(SelectFieldHostComponent);
    fixture.detectChanges();
  });

  it('disables projected actions together with the select', () => {
    const button = fixture.nativeElement.querySelector('#create-customer') as HTMLButtonElement;
    expect(button.matches(':disabled')).toBeFalse();

    fixture.componentInstance.disabled.set(true);
    fixture.detectChanges();

    expect(button.matches(':disabled')).toBeTrue();
  });
});
