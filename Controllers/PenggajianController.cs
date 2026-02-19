using LKS_ITSSA_2025.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Identity.Client;

namespace LKS_ITSSA_2025.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class PenggajianController : ControllerBase
    {
        EsensiAppContext context = new EsensiAppContext();
        [HttpGet("me")]
        [Authorize]
        public IActionResult gaji()
        {
            var idUserString = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (string.IsNullOrEmpty(idUserString) || !int.TryParse(idUserString, out int idUser))
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            }
            var getGaji = context.Penggajians.Where(d => d.UserId == idUser && d.SudahSelesai == 0).ToList();
            var idGaji = getGaji.Select(d => d.GajiId);
            var Gaji = context.Gajis.FirstOrDefault(d => d.UserId == idUser)?.Gaji1;
            if (getGaji != null || !getGaji.Any())
            {
                var respon = getGaji.Select(d => new {
                    gajiId = d.GajiId,
                    userId = d.UserId,
                    gaji_pokok = Gaji,
                    bonus = d.Bonus,
                    pelanggaran = d.Pelanggaran,
                    total = d.Total,
                    isSelesai = d.SudahSelesai
                }).ToList();
                return Ok(respon);
            } else
            {
                return BadRequest("No Salary Record!");
            }
        }

        [HttpPut("{id}")]
        [Authorize]
        public IActionResult result(int id)
        {
            var getGaji = context.Penggajians.FirstOrDefault(d => d.UserId == id && d.SudahSelesai == 0);
            if (getGaji != null)
            {
                getGaji.SudahSelesai = 1;
                context.Penggajians.Update(getGaji);
                context.SaveChanges();
                return Ok("Gajimu Sudah diambil!");
            }
            else
            {
                return BadRequest("Gaji Tidak Ditemukan");
            }
        }
    }
}