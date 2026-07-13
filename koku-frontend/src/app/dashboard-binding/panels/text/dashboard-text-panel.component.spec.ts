import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { DashboardTextPanelComponent } from './dashboard-text-panel.component';

describe('DashboardTextPanelComponent', () => {
  it('derives display colors without mutating its content', async () => {
    await TestBed.configureTestingModule({ imports: [DashboardTextPanelComponent] })
      .overrideComponent(DashboardTextPanelComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DashboardTextPanelComponent);
    const content = {
      '@type': 'text',
      color: 'SUCCESS',
      progressDetails: [{ headline: 'Done', headlineColor: 'ERROR' }],
    } as any;
    fixture.componentRef.setInput('content', content);
    fixture.detectChanges();
    expect(fixture.componentInstance.contentWithColors()).toEqual(
      expect.objectContaining({
        _color: expect.any(String),
        progressDetails: [expect.objectContaining({ _headlineColor: expect.any(String) })],
      }),
    );
    expect(content).not.toHaveProperty('_color');
  });
});
