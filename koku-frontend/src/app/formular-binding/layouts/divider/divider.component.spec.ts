import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { DividerComponent } from './divider.component';

describe('DividerComponent', () => {
  it('binds loading, disabled and text state', async () => {
    await TestBed.configureTestingModule({ imports: [DividerComponent] })
      .overrideComponent(DividerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DividerComponent);
    fixture.componentRef.setInput('loading', 'true');
    fixture.componentRef.setInput('disabled', 'true');
    fixture.componentRef.setInput('text', 'Section');
    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(true);
    expect(fixture.componentInstance.disabled()).toBe(true);
    expect(fixture.componentInstance.text()).toBe('Section');
  });
});
