import { Component, inject } from '@angular/core';
import { NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink, NgClass],
  templateUrl: './home.html'
})
export class Home {
  protected auth = inject(AuthService);
}
