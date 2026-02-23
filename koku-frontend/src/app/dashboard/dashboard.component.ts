import { Component, inject, InjectionToken, input, OnChanges, OnDestroy, signal, SimpleChanges } from '@angular/core';
import { DashboardContainerRendererComponent } from './container-renderer/dashboard-container-renderer.component';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../toast/toast.service';
import { Observable, Subscription } from 'rxjs';

export interface DashboardContentSetup {
  containerRegistry: Partial<
    Record<
      KokuDto.AbstractDashboardContainer['@type'] | string,
      {
        componentType: any;
        inputBindings?(instance: any, content: KokuDto.AbstractDashboardContainer): Record<string, any>;
        outputBindings?(instance: any, content: KokuDto.AbstractDashboardContainer): Record<string, any>;
      }
    >
  >;
  panelRegistry: Partial<
    Record<
      KokuDto.AbstractDashboardPanel['@type'] | string,
      {
        componentType: any;
        inputBindings?(instance: any, content: KokuDto.AbstractDashboardPanel): Record<string, any>;
        outputBindings?(instance: any, content: KokuDto.AbstractDashboardPanel): Record<string, any>;
      }
    >
  >;
}

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
  styleUrls: ['./dashboard.component.css'],
  imports: [DashboardContainerRendererComponent],
})
export class DashboardComponent implements OnDestroy, OnChanges {
  dashboardUrl = input.required<string>();
  contentSetup = input.required<DashboardContentSetup>();
  dashboardData = signal<KokuDto.DashboardViewDto | null>(null);
  private lastDashboardSubscription: Subscription | undefined;

  httpClient = inject(HttpClient);
  toastService = inject(ToastService);
  dashboardPluginsConfig = inject(DASHBOARD_PLUGIN, {
    optional: true,
  });

  private pluginInstances: DashboardPlugin[] = [];

  ngOnChanges(changes: SimpleChanges) {
    if (changes['dashboardUrl']) {
      this.loadDashboard().subscribe();
    }
  }

  private loadDashboard() {
    return new Observable(() => {
      const dashboardUrlSnapshot = this.dashboardUrl();
      if (dashboardUrlSnapshot) {
        if (this.lastDashboardSubscription && !this.lastDashboardSubscription.closed) {
          this.lastDashboardSubscription.unsubscribe();
        }
        this.lastDashboardSubscription = this.httpClient.get<KokuDto.DashboardViewDto>(dashboardUrlSnapshot).subscribe({
          next: (dashboardData) => {
            this.dashboardData.set(dashboardData);
          },
          error: () => {
            this.toastService.add('Fehler beim Laden der Daten', 'error', undefined, Number.POSITIVE_INFINITY);
          },
        });
      }
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
    for (const currentPluginInstance of this.pluginInstances || []) {
      currentPluginInstance.destroy?.();
    }
  }
}
