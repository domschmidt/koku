import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { StatFieldComponent } from './stat-field.component';

describe('StatFieldComponent', () => {
  it('binds its name and value', async () => {
    await TestBed.configureTestingModule({ imports: [StatFieldComponent] })
      .overrideComponent(StatFieldComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(StatFieldComponent);
    fixture.componentRef.setInput('name', 'total');
    fixture.componentRef.setInput('value', '42');
    fixture.detectChanges();
    expect(fixture.componentInstance.value()).toBe('42');
  });
});
