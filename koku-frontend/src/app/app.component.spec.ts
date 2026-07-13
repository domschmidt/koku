import { TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import { describe, expect, it } from 'vitest';
import { AppComponent } from './app.component';
import { ThemingService } from './theme/theming.service';

describe('AppComponent', () => {
  it('creates with its production component providers', async () => {
    await TestBed.configureTestingModule({ imports: [AppComponent] })
      .overrideComponent(AppComponent, { set: { template: '' } })
      .compileComponents();
    expect(TestBed.createComponent(AppComponent).componentInstance).toBeTruthy();
  });

  it('reflects theme updates', async () => {
    const theme = { theme: new BehaviorSubject<'koku-light' | 'koku-dark'>('koku-light') };
    await TestBed.configureTestingModule({ imports: [AppComponent] })
      .overrideComponent(AppComponent, {
        set: { template: '', providers: [{ provide: ThemingService, useValue: theme }] },
      })
      .compileComponents();
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance.theme).toBe('koku-light');
    theme.theme.next('koku-dark');
    expect(fixture.componentInstance.theme).toBe('koku-dark');
  });
});
