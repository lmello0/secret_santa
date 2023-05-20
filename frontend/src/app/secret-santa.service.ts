import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IRaffle } from './raffle/raffle';
import { IParticipant } from './raffle/participant';

@Injectable({
  providedIn: 'root'
})
export class SecretSantaService {
  private readonly API = 'http://localhost:3000';

  constructor(private http: HttpClient) { }

  getRaffle(raffleCode: String): Observable<IRaffle> {
    const url = `${this.API}/getRaffle/${raffleCode}`;

    return this.http.get<IRaffle>(url);
  }

  createRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url = `${this.API}/createRaffle/${raffle.code}`;

    return this.http.post<IRaffle>(url, raffle);
  }

  saveRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url = `${this.API}/updateRaffle`;

    return this.http.put<IRaffle>(url, raffle);
  }

  deleteRaffle(code: String): Observable<IRaffle> {
    const url = `${this.API}/deleteRaffle/${code}`;

    return this.http.delete<IRaffle>(url);
  }

  startRaffle(code: String): Observable<IRaffle> {
    const url = `${this.API}/startRaffle`;

    return this.http.post<IRaffle>(url, code);
  }

  addParticipant(code: String, participant: IParticipant): Observable<IParticipant> {
    const url = `${this.API}/updateRaffle/${code}/participant/add`;

    return this.http.put<IParticipant>(url, participant);
  }

  deleteParticipant(code: String, id: number): Observable<IParticipant> {
    const url = `${this.API}/updateRaffle/${code}/participant/remove/${id}`;

    return this.http.delete<IParticipant>(url);
  }

  updateParticipant(code: String, participant: IParticipant): Observable<IParticipant> {
    const url = `${this.API}/updateRaffle/${code}/participant/edit/${participant.id!}`;

    return this.http.put<IParticipant>(url, participant);
  }
}
