using LKS_ITSSA_2025.Models;
using LKS_ITSSA_2025.Schema;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Identity.Client;
using System.Globalization;

namespace LKS_ITSSA_2025.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AbsensiController : ControllerBase
    {
        EsensiAppContext context = new EsensiAppContext();
        [HttpGet("Rekap/me")]
        [Authorize]
        public IActionResult getAbsen()
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }

            var idAbsensi = context.AbsenUsers?.Include(tp => tp.User).FirstOrDefault(f => f.UserId == idUser);
            if (idAbsensi == null)
            {
                return NotFound("Task not found for this user.");
            }
            var Hadir = context.AbsenUsers?.Where(d => d.UserId == idAbsensi.UserId && d.StatusId == 1).Count();
            var Alpha = context.AbsenUsers?.Where(d => d.UserId == idAbsensi.UserId && d.StatusId == 4).Count();
            var Izin = context.AbsenUsers?.Where(d => d.UserId == idAbsensi.UserId && d.StatusId == 5).Count();
            var Sakit = context.AbsenUsers?.Where(d => d.UserId == idAbsensi.UserId && d.StatusId == 6).Count();
            return Ok(new
            {
                UserId = idAbsensi.UserId,
                Hadir = Hadir,
                TidakHadir = Alpha,
                Izin = Izin,
                Sakit = Sakit
            });
        }

        [HttpPost("Harian/me/masuk")]
        [Authorize]
        public IActionResult postabsen(RequestAbsensiScheme req)
        {
            AbsenUser absenUser;
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }

            var userid = context.AbsenUsers?.Include(tp => tp.User).FirstOrDefault(f => f.UserId == idUser);
            if (userid == null)
            {
                return NotFound("Task not found for this user.");
            }
            else
            {
                if (req.status_id == 1)
                {
                    absenUser = new AbsenUser()
                    {
                        UserId = userid.UserId,
                        StatusId = req.status_id,
                        JamKeluar = null,
                        JamMasuk = DateTime.Now,
                        SelfieMasuk = req.selfie,
                        Tanggal = DateOnly.FromDateTime(DateTime.Now)
                    };
                }
                else
                {
                    absenUser = new AbsenUser()
                    {
                        UserId = userid.UserId,
                        StatusId = req.status_id,
                        JamKeluar = null,
                        JamMasuk = null,
                        SelfieMasuk = req.selfie,
                        Tanggal = DateOnly.FromDateTime(DateTime.Now)
                    };
                }
                context.AbsenUsers?.Add(absenUser);
                context.SaveChanges();
                return Ok(absenUser);
            }
        }

        [HttpPut("Harian/me/keluar")]
        [Authorize]
        public IActionResult PostAbsen(ReqKeluarScheme sch)
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }

            var userid = context.AbsenUsers?.Include(tp => tp.User).FirstOrDefault(f => f.UserId == idUser && f.Id == sch.absen_id);
            if (userid == null)
            {
                return NotFound("Task not found for this user.");
            }

            var getStatusID = context.AbsenUsers?.FirstOrDefault(f => f.Id == sch.absen_id)?.StatusId;
            if (getStatusID != 1)
            {
                return BadRequest("Anda Hari ini tidak ke Kantor, Absen Anda bukan masuk!");
            }
            else
            {
                userid.JamKeluar = DateTime.Now;
                userid.SelfieKeluar = sch.selfieKeluar;
                context.AbsenUsers?.Update(userid);
                context.SaveChanges();

                return Ok(userid);
            }
        }

        [HttpGet("get/detailby/{id}")]
        [Authorize]
        public IActionResult GetAbsen(int id)
        {
            var absenDetail = context.AbsenUsers.Where(f => f.Id == id).Select(
                c => new
                {
                    id = c.Id,
                    Nama = c.User.Nama,
                    Status = c.Status.Status,
                    Tanggal = c.JamMasuk.HasValue? c.JamMasuk.Value.ToString("dddd, dd MMM yyyy", new CultureInfo("id-ID")) : (c.Tanggal.HasValue ? c.Tanggal.Value.ToString("dddd, dd MMM yyyy", new CultureInfo("id-ID")) : "-"),
                    FotoMasuk = c.SelfieMasuk,
                    FotoKeluar = c.SelfieKeluar
                }).FirstOrDefault();
            return Ok(absenDetail);
        }

        [HttpGet("user")]
        [Authorize]
        public IActionResult GetAbsen()
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }
            var absenDetail = context.AbsenUsers.Where(f => f.UserId == Convert.ToInt32(idUserString))
                .Select(c => new
                {
                    c.Id,
                    c.UserId,
                    c.StatusId,
                    Tanggal = c.JamMasuk.HasValue ? c.JamMasuk.Value.ToString("dddd, dd MMM yyyy", new CultureInfo("id-ID")) : (c.Tanggal.HasValue ? c.Tanggal.Value.ToString("dddd, dd MMM yyyy", new CultureInfo("id-ID")) : "-"),
                    WaktuMasuk = c.JamMasuk.HasValue ? c.JamMasuk.Value.ToString("HH:mm") : "-",
                    WaktuKeluar = c.JamKeluar.HasValue ? c.JamKeluar.Value.ToString("HH:mm") : "-",
                })
                .ToList();
            return Ok(absenDetail);
        }
    }
}
