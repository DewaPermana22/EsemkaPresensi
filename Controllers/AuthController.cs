using LKS_ITSSA_2025.Models;
using LKS_ITSSA_2025.Schema;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace LKS_ITSSA_2025.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        readonly IConfiguration _configuration;
        readonly EsensiAppContext _esensi;

        public AuthController(IConfiguration configuration, EsensiAppContext esensi)
        {
            _configuration = configuration;
            _esensi = esensi;
        }

        private string GenereateToken(User user)
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration["token"]!));
            var claim = new[]
            {
                new Claim("UserID", user.Id.ToString(), ClaimValueTypes.Integer)
            };
            var token = new JwtSecurityToken(
                claims: claim,
                expires: DateTime.Now.AddYears(1),
                signingCredentials: new SigningCredentials(key, SecurityAlgorithms.HmacSha256)
                );
            return new JwtSecurityTokenHandler().WriteToken(token);
        }

        [HttpPost("Register")]
        public IActionResult register(RegisterSchema sch)
        {
           var kode = _esensi.KodeReverals.FirstOrDefault( c => c.Code == sch.kodereveral)?.Id;
            var regAuth = _esensi.Users.FirstOrDefault(d => d.Email == sch.email || d.Nama == sch.username);
            if (regAuth != null)
            {
                return BadRequest("User Already Exists");
            }

            if (kode == null)
            {
                return BadRequest("Invalid Referral Code");
            }
            if (regAuth == null && kode != null)
            {
                User user = new User()
                {
                    Email = sch.email,
                    Nama = sch.username,
                    Password = sch.password,
                    KodeReveral = Convert.ToInt32(kode),
                    EncryptBiometric = sch.Biometrics
                };

                _esensi.Add(user);
                _esensi.SaveChanges();
                var findKode = _esensi.KodeReverals.FirstOrDefault(d => d.Id == user.KodeReveral && d.UserId == null);

                if (findKode != null)
                {
                    // Update UserId pada kode referal yang ditemukan
                    findKode.UserId = user.Id;
                    _esensi.KodeReverals.Update(findKode);
                }
                _esensi.SaveChanges();

                string tokenUsr = GenereateToken(user);
                return Ok(tokenUsr);
            }
            else
            {
                return BadRequest("User Already Exist");
            }
        }

        [HttpPost("Login")]
        public IActionResult login(LoginSchema sch)
        {
            var kodeReveral = _esensi.KodeReverals.Where(f => f.Code == sch.kodereveral).FirstOrDefault()?.Id;
            var regAuth = _esensi.Users.FirstOrDefault(d => d.Email == sch.email && d.Password == sch.password && d.KodeReveral == kodeReveral);
            if (regAuth == null)
            {
                return BadRequest("User Tidak Ditemukan!");
            }
            else
            {
                string tokenUsr = GenereateToken(regAuth);
                return Ok(tokenUsr);
            }
        }

        [Authorize]
        [HttpGet("me")]
        public IActionResult me()
        {
            var idUser = User.Claims.FirstOrDefault(c => c.Type == "UserID")?.Value;
            if (idUser == null)
            {
                return BadRequest("Invalid Token! or Token Not Found!");
            } else
            {
                var detailUser = _esensi.Users.Where(f => f.Id == Convert.ToInt32(idUser)).Select(
                    f => new
                    {
                        f.Id,
                        f.Nama,
                        f.Email,
                        f.ProfilPict,
                        f.EncryptBiometric,
                        f.KodeReveralNavigation.Code
                    });
                return Ok(detailUser);
            }
           
        }
    }
}
