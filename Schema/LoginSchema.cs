using System.ComponentModel.DataAnnotations;

namespace LKS_ITSSA_2025.Schema
{
    public class LoginSchema
    {
        [Required]
        public string email { get; set; }
        [Required]
        public string password { get; set; }
        [Required]
        public string kodereveral { get; set; }
        public string Biometrics { get; set; }
    }
}
