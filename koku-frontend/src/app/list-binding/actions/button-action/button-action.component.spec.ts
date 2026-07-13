import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ButtonActionComponent } from './button-action.component';

describe('ButtonActionComponent', () => {
  it('binds action state and emits click events', async () => {
    await TestBed.configureTestingModule({ imports: [ButtonActionComponent] })
      .overrideComponent(ButtonActionComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ButtonActionComponent);
    fixture.componentRef.setInput('value', { '@type': 'http-call' });
    fixture.componentRef.setInput('register', {});
    fixture.componentRef.setInput('contentSetup', {});
    fixture.componentRef.setInput('loading', 'true');
    fixture.detectChanges();
    const clicked = vi.fn();
    fixture.componentInstance.clicked.subscribe(clicked);
    const event = new MouseEvent('click');
    fixture.componentInstance.clicked.emit(event);
    expect(fixture.componentInstance.loading()).toBe(true);
    expect(clicked).toHaveBeenCalledWith(event);
  });
});
