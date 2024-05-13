import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IRaffle } from './raffle/raffle';

@Injectable({
  providedIn: 'root'
})
export class SecretSantaService {
  private readonly API = 'http://localhost:8080/draw';
  private headers = new HttpHeaders({ 'X-API-KEY': process.env['FRONT_API_KEY'] || '' });

  constructor(private http: HttpClient) { }

  getRaffle(raffleCode: String): Observable<IRaffle> {
    const url = `${this.API}/${raffleCode}`;

    return this.http.get<IRaffle>(url, { headers: this.headers });
  }

  createRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url = `${this.API}`;

    return this.http.post<IRaffle>(url, raffle, { headers: this.headers });
  }

  saveRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url = `${this.API}/${raffle.code}`;

    return this.http.put<IRaffle>(url, raffle, { headers: this.headers });
  }

  deleteRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url = `${this.API}/${raffle.code}`;

    return this.http.delete<IRaffle>(url, { headers: this.headers });
  }

  startRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url = `${this.API}/${raffle.code}`

    return this.http.post<IRaffle>(url, raffle, { headers: this.headers });
  }
}
