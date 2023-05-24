import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { AboutComponent } from './about/about.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { RaffleComponent } from './raffle/raffle.component';
import { MenuComponent } from './raffle/menu/menu.component';
import { ParticipantsListComponent } from './raffle/participants-list/participants-list.component';
import { ParticipantComponent } from './raffle/participant/participant.component';
import { ModalModule } from './modal/modal.module';
import { NavbarComponent } from './navbar/navbar.component';
import { ToastNotificationComponent } from './toast-notification/toast-notification.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    AboutComponent,
    NotFoundComponent,
    RaffleComponent,
    MenuComponent,
    ParticipantsListComponent,
    ParticipantComponent,
    NavbarComponent,
    ToastNotificationComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule,
    ModalModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
