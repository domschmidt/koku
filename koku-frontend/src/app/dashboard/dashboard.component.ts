import {
  Component,
  inject,
  InjectionToken,
  input,
  OnChanges,
  OnDestroy,
  Signal,
  signal,
  SimpleChanges,
} from '@angular/core';
import { DashboardContentRendererComponent } from './content-renderer/dashboard-content-renderer.component';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../toast/toast.service';
import { Subscription } from 'rxjs';
import { DynamicRenderRecipe } from '../dynamic-host/dynamic-host.directive';

export type DashboardContent = KokuDto.AbstractDashboardContainer | KokuDto.AbstractDashboardPanel;

export interface DashboardContentRenderContext<TContent extends DashboardContent = DashboardContent> {
  content: Signal<TContent>;
  contentRegistry: Signal<DashboardContentRegistry>;
}

export type DashboardContentRecipeFactory<TContent extends DashboardContent = any> = (
  context: DashboardContentRenderContext<TContent>,
) => DynamicRenderRecipe;

export type DashboardContentRegistry = Partial<Record<string, DashboardContentRecipeFactory>>;

export interface DashboardPlugin {
  destroy(): void;
}

export type DashboardPluginFactory = (instance: DashboardComponent) => DashboardPlugin;

export const DASHBOARD_PLUGIN = new InjectionToken<DashboardPluginFactory | DashboardPluginFactory[]>(
  'Dashboard Plugins',
);

@Component({
  selector: 'koku-dashboard',
  templateUrl: './dashboard.component.html',
  imports: [DashboardContentRendererComponent],
})
export class DashboardComponent implements OnDestroy, OnChanges {
  dashboardUrl = input.required<string>();
  contentRegistry = input.required<DashboardContentRegistry>();
  dashboardData = signal<KokuDto.DashboardViewDto | null>(null);
  private lastDashboardSubscription: Subscription | undefined;

  readonly httpClient = inject(HttpClient);
  readonly toastService = inject(ToastService);
  readonly dashboardPluginsConfig = inject(DASHBOARD_PLUGIN, {
    optional: true,
  });

  private readonly pluginInstances: DashboardPlugin[];

  ngOnChanges(changes: SimpleChanges) {
    if (changes['dashboardUrl']) {
      this.loadDashboard();
    }
  }

  private loadDashboard() {
    const dashboardUrlSnapshot = this.dashboardUrl();
    if (!dashboardUrlSnapshot) {
      this.dashboardData.set(null);
      return;
    }
    this.lastDashboardSubscription?.unsubscribe();
    this.lastDashboardSubscription = this.httpClient.get<KokuDto.DashboardViewDto>(dashboardUrlSnapshot).subscribe({
      next: (dashboardData) => {
        this.dashboardData.set(dashboardData);
      },
      error: () => {
        this.dashboardData.set(null);
        this.toastService.add('Fehler beim Laden der Daten', 'error', undefined, Number.POSITIVE_INFINITY);
      },
    });
  }

  constructor() {
    const pluginInstances = [];
    let pluginsConfig = this.dashboardPluginsConfig;
    if (pluginsConfig) {
      if (!Array.isArray(pluginsConfig)) {
        pluginsConfig = [pluginsConfig];
      }
      for (const currentPlugin of pluginsConfig || []) {
        pluginInstances.push(currentPlugin(this));
      }
    }
    this.pluginInstances = pluginInstances;
  }

  ngOnDestroy(): void {
    this.lastDashboardSubscription?.unsubscribe();
    for (const currentPluginInstance of this.pluginInstances || []) {
      currentPluginInstance.destroy?.();
    }
  }
}
