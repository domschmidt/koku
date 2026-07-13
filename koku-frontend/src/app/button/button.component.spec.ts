import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ButtonComponent } from './button.component';

describe('ButtonComponent', () => {
  it('indexes styles, emits clicks and resolves browser link targets', async () => {
    const open = vi.spyOn(globalThis, 'open').mockImplementation(() => null);
    await TestBed.configureTestingModule({ imports: [ButtonComponent] })
      .overrideComponent(ButtonComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ButtonComponent);
    fixture.componentRef.setInput('styles', ['PRIMARY', 'OUTLINE']);
    fixture.componentRef.setInput('href', 'https://example.test');
    fixture.componentRef.setInput('hrefTarget', 'BLANK');
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(component.indexedStyles()).toEqual(new Set(['PRIMARY', 'OUTLINE']));
    const clicked = vi.fn();
    component.clicked.subscribe(clicked);
    const event = new Event('click');
    component.onClickRaw(event);
    expect(clicked).toHaveBeenCalledWith(event);
    expect(open).toHaveBeenCalledWith('https://example.test', '_blank');
    fixture.componentRef.setInput('hrefTarget', 'SELF');
    fixture.detectChanges();
    component.onClickRaw(event);
    expect(open).toHaveBeenLastCalledWith('https://example.test', '_self');
    fixture.componentRef.setInput('href', undefined);
    fixture.detectChanges();
    component.onClickRaw(event);
    expect(open).toHaveBeenCalledTimes(2);
    vi.restoreAllMocks();
  });
});
